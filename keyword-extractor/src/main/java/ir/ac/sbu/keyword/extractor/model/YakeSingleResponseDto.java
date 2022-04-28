package ir.ac.sbu.keyword.extractor.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public class YakeSingleResponseDto {

    // NOTE: score shows keyword (Problem of Yake server implementation)
    @JsonProperty("score")
    private String keyword;

    // NOTE: ngram shows score of keyword (Problem of Yake server implementation)
    @JsonProperty("ngram")
    private double score;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        YakeSingleResponseDto that = (YakeSingleResponseDto) o;
        return Double.compare(that.score, score) == 0 && Objects.equals(keyword, that.keyword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyword, score);
    }

    @Override
    public String toString() {
        return "YakeSingleResponseDto{" +
                "keyword='" + keyword + '\'' +
                ", score=" + score +
                '}';
    }
}
