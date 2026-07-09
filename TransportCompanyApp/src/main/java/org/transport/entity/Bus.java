package org.transport.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Represents a bus vehicle used for passenger transport
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Bus extends Vehicle {

    @NotNull
    @Positive
    @Column(nullable = false)
    private Integer seatCapacity;
}
