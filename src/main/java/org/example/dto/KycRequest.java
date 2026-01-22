package org.example.dto;
import org.example.entity.enums.GovtIdType;

public class KycRequest {
    public String address;
    public String dateOfBirth;
    public GovtIdType govtIdType;
    public String govtIdNumber;
}