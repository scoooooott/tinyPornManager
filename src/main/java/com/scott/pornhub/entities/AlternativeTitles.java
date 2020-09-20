package com.scott.pornhub.entities;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AlternativeTitles {

    @SerializedName(value = "titles", alternate = {"results"})
    public List<AlternativeTitle> titles;

    public Integer id;

}
