package com.cashujdk.nut02;

import java.util.List;
import java.util.Map;

import com.cashujdk.nut00.Proof;

public class FeeHelper {
    public static int ComputeFee(List<Proof> proofsToSpend, Map<String, Integer> keysetFees)
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
