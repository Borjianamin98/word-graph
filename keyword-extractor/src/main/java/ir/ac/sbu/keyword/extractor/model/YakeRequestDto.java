package ir.ac.sbu.keyword.extractor.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class YakeRequestDto {

    @JsonProperty("language")
    private String language;

    @JsonProperty("max_ngram_size")
    private int maxNgramSize;

    @JsonProperty("number_of_keywords")
    private int numberOfKeywords;

    @JsonProperty("text")
    private String text;

    public YakeRequestDto() {
        // Foy Jackson binding
    }

    public YakeRequestDto(String language, int maxNgramSize, int numberOfKeywords, String text) {
        this.language = language;
        this.maxNgramSize = maxNgramSize;
        this.numberOfKeywords = numberOfKeywords;
        this.text = text;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getMaxNgramSize() {
        return maxNgramSize;
    }

    public void setMaxNgramSize(int maxNgramSize) {
        this.maxNgramSize = maxNgramSize;
    }

    public int getNumberOfKeywords() {
        return numberOfKeywords;
    }

    public void setNumberOfKeywords(int numberOfKeywords) {
        this.numberOfKeywords = numberOfKeywords;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
