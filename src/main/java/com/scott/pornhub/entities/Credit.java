package com.scott.pornhub.entities;

import com.scott.pornhub.enumerations.CreditType;
import com.scott.pornhub.enumerations.MediaType;

public class Credit {

    public CreditType credit_type;
    public String department;
    public String job;
    public CreditMedia media;
    public MediaType media_type;
    public String id;
    public BasePerson person;

}
