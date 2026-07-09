package org.transport.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

// Represents a truck vehicle used for goods transport
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Truck extends Vehicle {

    @NotNull
    @Positive
    @Column(nullable = false)
    private BigDecimal maxLoadKg;
}
