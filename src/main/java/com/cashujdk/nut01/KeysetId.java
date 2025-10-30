package com.cashujdk.nut01;

public class KeysetId {
    private String _id;

    public String get_id() {
        return _id;
    }
    public void set_id(String id) {
        if (id.length() != 66 && id.length() != 16 && id.length() != 12)
        {
            throw new IllegalArgumentException("KeysetId must be 66, 16 or 12 (legacy) characters long");
        }

        _id = id;
    }
}
