package com.cashujdk.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.cashujdk.errors.CashuExceptions;
import com.cashujdk.nut00.Proof;

public class ProofSelector {

    private static final int MAX_TRIALS = 60;
    private static final int MAX_OVRPCT = 0;
    private static final int MAX_OVRAMT = 0;
    private static final int MAX_TIMEMS = 1000;
    private static final int MAX_P2SWAP = 5000;

    private final Optional<Map<String, Integer>> keysetFees;

    public ProofSelector(Optional<Map<String, Integer>> _keysetFees) {
        keysetFees = _keysetFees;
    }

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

        @Override 
        public boolean equals(Object obj) {
            if (!(obj instanceof ProofWithFee)) return false;
            return this.proof.equals(((ProofWithFee)obj).proof);
        }

        @Override
        public int hashCode() {
            return this.proof.hashCode();
        }
    }

    // Calculate delta
    private double calculateDelta(double amount, double amountToSend, double feePPK, boolean includeFees) {
        double netSum = sumExFees(amount, feePPK, includeFees);
        if (netSum < amountToSend) return Double.POSITIVE_INFINITY; // no good
        return netSum - amountToSend;
    }

    // Calculate net amount after fees
    private double sumExFees(double amount, double feePPK, boolean includeFees) {
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
        // Create defensive copy of input list
        List<Proof> workingProofs = new ArrayList<>(proofs);
        
        // If amount to send is invalid, return early
        if (amountToSend <= 0) {
            return new Pair<>(workingProofs, new ArrayList<>());
        }
        
        // Separate zero amount proofs from non-zero proofs
        List<Proof> zeroAmountProofs = workingProofs.stream()
            .filter(p -> p.amount == 0)
            .collect(Collectors.toCollection(ArrayList::new));
            
        List<Proof> nonZeroProofs = workingProofs.stream()
            .filter(p -> p.amount > 0)
            .collect(Collectors.toCollection(ArrayList::new));

        // If no non-zero proofs, return early
        if (nonZeroProofs.isEmpty()) {
            return new Pair<>(workingProofs, new ArrayList<>());
        }

        // Init vars
        boolean exactMatch = false; // Allows close match (> amountToSend + fee)
        Timer timer = new Timer(); // Start the clock
        List<ProofWithFee> bestSubset = null;
        double bestDelta = Double.POSITIVE_INFINITY;
        double bestAmount = 0;
        double bestFeePPK = 0;

        // Pre-processing: Convert all non-zero proofs to ProofWithFee objects
        List<ProofWithFee> proofWithFees = new ArrayList<>();
        double totalAmount = 0;
        double totalFeePPK = 0;

        for (Proof p : nonZeroProofs) {
            double ppkfee = (includeFees) ? getProofFeePPK(p) : 0;
            double exFee = includeFees ? (p.amount - ppkfee / 1000.0) : p.amount;
            ProofWithFee obj = new ProofWithFee(p, exFee, ppkfee);
            proofWithFees.add(obj);
            totalAmount += p.amount;
            totalFeePPK += ppkfee;
        }

        // Check if we have enough funds after fees
        double totalNetSum = sumExFees(totalAmount, totalFeePPK, includeFees);
        if (amountToSend > totalNetSum) {
            // Special case: If we have exact amount without fees, but not enough with fees,
            // try without fees
            if (includeFees && totalAmount >= amountToSend) {
                return selectProofsToSend(proofs, amountToSend, false);
            }
            return new Pair<>(workingProofs, new ArrayList<>());
        }

        // Sort proofs by exFee ascending
        proofWithFees.sort(Comparator.comparingDouble(a -> a.exFee));

        // Remove proofs too large to be useful
        List<ProofWithFee> spendableProofs = new ArrayList<>(proofWithFees);
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
            spendableProofs = new ArrayList<>(spendableProofs.subList(0, endIndex));
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

            // Sort others by exFee for binary search
            others.sort(Comparator.comparingDouble(a -> a.exFee));

            // Generate a random order for accessing the trial subset ('S')
            List<Integer> indices = new ArrayList<>();
            for (int i = 0; i < S.size(); i++) {
                indices.add(i);
            }
            Collections.shuffle(indices);
            indices = new ArrayList<>(indices.subList(0, Math.min(MAX_P2SWAP, indices.size())));

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

                // Find a better replacement proof (objQ) using binary search
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

                // Found perfect match?
                if (bestDelta == 0) break;

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
                    throw new CashuExceptions.ProofSelectionException("Proof selection took too long. Try again with a smaller proof set.");
                } else {
                    System.out.println("Proof selection took too long. Returning best selection so far.");
                    break;
                }
            }
        }

        // If we found a valid solution
        if (bestSubset != null && bestDelta < Double.POSITIVE_INFINITY) {
            // Extract selected proofs
            List<Proof> selectedProofs = bestSubset.stream()
                .map(pwf -> pwf.proof)
                .collect(Collectors.toCollection(ArrayList::new));

            // Calculate remaining proofs
            Set<Proof> selectedSet = new HashSet<>(selectedProofs);
            List<Proof> remainingProofs = new ArrayList<>();
            
            // Add non-selected non-zero proofs
            for (Proof p : nonZeroProofs) {
                if (!selectedSet.contains(p)) {
                    remainingProofs.add(p);
                }
            }
            
            // Add all zero amount proofs
            remainingProofs.addAll(zeroAmountProofs);

            System.out.println("Proof selection took " + timer.elapsed() + "ms");
            return new Pair<>(remainingProofs, selectedProofs);
        }

        // No valid solution found, try without fees if we were using them
        if (includeFees) {
            return selectProofsToSend(proofs, amountToSend, false);
        }

        return new Pair<>(workingProofs, new ArrayList<>());
    }

    // Get proof fee per key (PPK)
    private double getProofFeePPK(Proof proof)
    throws NoSuchElementException, ClassCastException, NullPointerException {
        Integer ppkFee = keysetFees.get().get(proof.keysetId);
        if (ppkFee == null) {
            throw new RuntimeException("Cannot get input fee for keyset " + proof.keysetId);
        }
        return ppkFee;
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
