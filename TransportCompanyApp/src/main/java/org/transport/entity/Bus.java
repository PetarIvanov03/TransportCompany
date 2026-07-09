package org.transport.entity;

import jakarta.persistence.Entity;
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

    @Positive
    private Integer seatCapacity;
}
