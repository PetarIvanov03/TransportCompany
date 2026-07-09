package org.transport.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.transport.entity.enums.TankerCargoType;

import java.math.BigDecimal;

// Represents a tanker vehicle used for liquid or bulk cargo transport
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tanker extends Vehicle {

    @NotNull
    @Positive
    @Column(nullable = false)
    private BigDecimal capacityLiters;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TankerCargoType permittedCargoType;
}
