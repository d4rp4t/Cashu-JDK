package com.project.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.project.nut00.Proof;

public class ProofSelector {

    private static final int MAX_TRIALS = 60;
    private static final int MAX_OVRPCT = 0;
    private static final int MAX_OVRAMT = 0;
    private static final int MAX_TIMEMS = 1000;
    private static final int MAX_P2SWAP = 5000;

    // Helper Functions
    private class ProofWithFee {
        Proof proof;
        double exFee;
        double ppkfee;

        ProofWithFee(Proof proof, double exFee, double ppkfee) {
            this.proof = proof;
            this.exFee = exFee;
            this.ppkfee = ppkfee;
        }
    }

    // Calculate delta
    private static double calculateDelta(double amount, double amountToSend, double feePPK, boolean includeFees) {
        double netSum = sumExFees(amount, feePPK, includeFees);
        if (netSum < amountToSend) return Double.POSITIVE_INFINITY; // no good
        return amount + feePPK / 1000 - amountToSend;
    }

    // Calculate net amount after fees
    private static double sumExFees(double amount, double feePPK, boolean includeFees) {
        return amount - (includeFees ? Math.ceil(feePPK / 1000f) : 0);
    }

    // Shuffle array for randomization
    private <T> List<T> shuffleArray(List<T> array) {
        List<T> shuffled = new ArrayList<>(array);
        Collections.shuffle(shuffled);
        return shuffled;
    }

    // Insert into array of ProofWithFee objects sorted by exFee
    void insertSorted(List<ProofWithFee> arr, ProofWithFee obj) {
        double value = obj.exFee;
        int left = 0, right = arr.size();
        while (left < right) {
            int mid = (left + right) / 2;
            if (arr.get(mid).exFee < value) left = mid + 1;
            else right = mid;
        }
        arr.add(left, obj);
    }

    // Performs a binary search on a sorted (ascending) array of ProofWithFee objects by exFee.
    private int binarySearchIndex(List<ProofWithFee> arr, double value, boolean lessOrEqual) {
        int left = 0, right = arr.size() - 1;
        int result = -1;
        while (left <= right) {
            int mid = (left + right) / 2;
            double midValue = arr.get(mid).exFee;
            if (lessOrEqual ? midValue <= value : midValue >= value) {
                result = mid;
                if (lessOrEqual) left = mid + 1;
                else right = mid - 1;
            } else {
                if (lessOrEqual) right = mid - 1;
                else left = mid + 1;
            }
        }
        return result;
    }

    public Pair<List<Proof>, List<Proof>> selectProofsToSend(List<Proof> proofs, int amountToSend, boolean includeFees) {
        // Init vars
        boolean exactMatch = false; // Allows close match (> amountToSend + fee)
        Timer timer = new Timer(); // Start the clock
        List<ProofWithFee> bestSubset = null;
        double bestDelta = Double.POSITIVE_INFINITY;
        double bestAmount = 0;
        double bestFeePPK = 0;

        // Pre-processing
        double totalAmount = 0;
        double totalFeePPK = 0;
        List<ProofWithFee> proofWithFees = new ArrayList<>();

        for (Proof p : proofs) {
            double ppkfee = getProofFeePPK(p);
            double exFee = includeFees ? p.amount - ppkfee / 1000 : p.amount;
            ProofWithFee obj = new ProofWithFee(p, exFee, ppkfee);
            if (!includeFees || exFee > 0) {
                totalAmount += p.amount;
                totalFeePPK += ppkfee;
            }
            proofWithFees.add(obj);
        }

        // Filter uneconomical proofs
        List<ProofWithFee> spendableProofs = includeFees
                ? proofWithFees.stream().filter(obj -> obj.exFee > 0).toList()
                : proofWithFees;

        // Sort by exFee ascending
        spendableProofs.sort(Comparator.comparingDouble(a -> a.exFee));

        // Remove proofs too large to be useful
        if (!spendableProofs.isEmpty()) {
            int endIndex;
            if (exactMatch) {
                int rightIndex = binarySearchIndex(spendableProofs, amountToSend, true);
                endIndex = rightIndex != -1 ? rightIndex + 1 : 0;
            } else {
                int biggerIndex = binarySearchIndex(spendableProofs, amountToSend, false);
                if (biggerIndex != -1) {
                    double nextBiggerExFee = spendableProofs.get(biggerIndex).exFee;
                    int rightIndex = binarySearchIndex(spendableProofs, nextBiggerExFee, true);
                    endIndex = rightIndex + 1; // rightIndex guaranteed non-null due to biggerIndex
                } else {
                    // Keep all proofs if all exFee < amountToSend
                    endIndex = spendableProofs.size();
                }
            }
            // Adjust totals for removed proofs
            for (int i = endIndex; i < spendableProofs.size(); i++) {
                totalAmount -= spendableProofs.get(i).proof.amount;
                totalFeePPK -= spendableProofs.get(i).ppkfee;
            }
            spendableProofs = spendableProofs.subList(0, endIndex);
        }

        // Validate using precomputed totals
        double totalNetSum = sumExFees(totalAmount, totalFeePPK, includeFees);
        if (amountToSend <= 0 || amountToSend > totalNetSum) {
            return new Pair<>(proofs, Collections.emptyList());
        }

        // Max acceptable amount for non-exact matches
        double maxOverAmount = Math.min(
                Math.min(
                    Math.ceil(amountToSend * (1 + MAX_OVRPCT / 100.0)),
                    amountToSend + MAX_OVRAMT
                ),
                totalNetSum
        );

        // RGLI algorithm: Runs multiple trials (up to MAX_TRIALS)
        for (int trial = 0; trial < MAX_TRIALS; trial++) {
            // PHASE 1: Randomized Greedy Selection
            List<ProofWithFee> S = new ArrayList<>();
            double amount = 0;
            double feePPK = 0;

            for (ProofWithFee obj : shuffleArray(spendableProofs)) {
                double newAmount = amount + obj.proof.amount;
                double newFeePPK = feePPK + obj.ppkfee;
                double netSum = sumExFees(newAmount, newFeePPK, includeFees);
                if (exactMatch && netSum > amountToSend) break;
                S.add(obj);
                amount = newAmount;
                feePPK = newFeePPK;
                if (netSum >= amountToSend) break;
            }

            // PHASE 2: Local Improvement
            Set<ProofWithFee> SSet = new HashSet<>(S);
            List<ProofWithFee> others = new ArrayList<>();
            for (ProofWithFee obj : spendableProofs) {
                if (!SSet.contains(obj)) {
                    others.add(obj);
                }
            }

            // Generate a random order for accessing the trial subset ('S')
            List<Integer> indices = new ArrayList<>();
            for (int i = 0; i < S.size(); i++) {
                indices.add(i);
            }
            Collections.shuffle(indices);
            indices = indices.subList(0, Math.min(MAX_P2SWAP, indices.size()));

            for (int i : indices) {
                double netSum = sumExFees(amount, feePPK, includeFees);
                if (netSum == amountToSend || (!exactMatch && netSum >= amountToSend && netSum <= maxOverAmount)) {
                    break;
                }

                // Get details for proof being replaced (objP)
                ProofWithFee objP = S.get(i);
                double tempAmount = amount - objP.proof.amount;
                double tempFeePPK = feePPK - objP.ppkfee;
                double tempNetSum = sumExFees(tempAmount, tempFeePPK, includeFees);
                double target = amountToSend - tempNetSum;

                // Find a better replacement proof (objQ)
                int qIndex = binarySearchIndex(others, target, exactMatch);
                if (qIndex != -1) {
                    ProofWithFee objQ = others.get(qIndex);
                    if (!exactMatch || objQ.exFee > objP.exFee) {
                        if (target >= 0 || objQ.exFee <= objP.exFee) {
                            S.set(i, objQ);
                            amount = tempAmount + objQ.proof.amount;
                            feePPK = tempFeePPK + objQ.ppkfee;
                            others.remove(qIndex);
                            insertSorted(others, objP);
                        }
                    }
                }
            }

            // Update best solution
            double delta = calculateDelta(amount, amountToSend, feePPK, includeFees);
            if (delta < bestDelta) {
                bestSubset = new ArrayList<>(S);
                bestSubset.sort(Comparator.comparingDouble(a -> -a.exFee)); // Copy & sort in descending order
                bestDelta = delta;
                bestAmount = amount;
                bestFeePPK = feePPK;

                // "PHASE 3": Final check to make sure we haven't overpaid fees
                List<ProofWithFee> tempS = new ArrayList<>(bestSubset); // Copy
                while (tempS.size() > 1 && bestDelta > 0) {
                    ProofWithFee objP = tempS.remove(tempS.size() - 1);
                    double tempAmount = bestAmount - objP.proof.amount;
                    double tempFeePPK = bestFeePPK - objP.ppkfee;
                    double tempDelta = calculateDelta(tempAmount, amountToSend, tempFeePPK, includeFees);
                    if (tempDelta == Double.POSITIVE_INFINITY) break;
                    if (tempDelta < bestDelta) {
                        bestSubset = new ArrayList<>(tempS);
                        bestDelta = tempDelta;
                        bestAmount = tempAmount;
                        bestFeePPK = tempFeePPK;
                    }
                }
            }

            // Check if solution is acceptable
            if (bestSubset != null && bestDelta < Double.POSITIVE_INFINITY) {
                double bestSum = sumExFees(bestAmount, bestFeePPK, includeFees);
                if (bestSum == amountToSend || (!exactMatch && bestSum >= amountToSend && bestSum <= maxOverAmount)) {
                    break;
                }
            }

            // Time limit reached?
            if (timer.elapsed() > MAX_TIMEMS) {
                if (exactMatch) {
                    throw new RuntimeException("Proof selection took too long. Try again with a smaller proof set.");
                } else {
                    System.out.println("Proof selection took too long. Returning best selection so far.");
                    break;
                }
            }
        }

        // Return Result
        if (bestSubset != null && bestDelta < Double.POSITIVE_INFINITY) {
            List<Proof> bestProofs = new ArrayList<>();
            for (ProofWithFee obj : bestSubset) {
                bestProofs.add(obj.proof);
            }
            Set<Proof> bestSubsetSet = new HashSet<>(bestProofs);
            List<Proof> keep = new ArrayList<>();
            for (Proof p : proofs) {
                if (!bestSubsetSet.contains(p)) {
                    keep.add(p);
                }
            }
            System.out.println("Proof selection took " + timer.elapsed() + "ms");
            return new Pair<>(keep, bestProofs);
        }
        return new Pair<>(proofs, Collections.emptyList());
    }

    // Placeholder for the method to get proof fee per key (PPK)
    private double getProofFeePPK(Proof proof) {
        // Implement the logic to calculate the proof fee per key (PPK)
        return 0; // Placeholder return value
    }

    // Timer class to measure elapsed time
    private static class Timer {
        private final long startTime;

        public Timer() {
            this.startTime = System.currentTimeMillis();
        }

        public long elapsed() {
            return System.currentTimeMillis() - startTime;
        }
    }
}


