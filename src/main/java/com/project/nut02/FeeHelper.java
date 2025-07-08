package com.project.nut02;

import com.project.nut00.Proof;

import java.util.Dictionary;
import java.util.List;

public class FeeHelper {
    public static int ComputeFee(List<Proof> proofsToSpend, Dictionary<String, Integer> keysetFees)
    {
        int sum = 0;

        for (Proof proof : proofsToSpend) {
            try{
                sum+=keysetFees.get(proof.keysetId);
            } catch (Exception e){
                //do nothing
            }
        }


        return (sum + 999) / 1000;
    }
}
