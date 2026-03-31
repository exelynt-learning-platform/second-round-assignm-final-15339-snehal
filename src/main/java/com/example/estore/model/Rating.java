package com.example.estore.model;



import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable   
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rating {
    private Double rate;
    private Integer count;
}
