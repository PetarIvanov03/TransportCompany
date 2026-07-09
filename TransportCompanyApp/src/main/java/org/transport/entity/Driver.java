package org.transport.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.transport.entity.enums.DriverQualification;

import java.util.List;
import java.util.Set;

// Represents a driver — a specialised employee who operates vehicles
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Driver extends Employee {

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "driver_qualifications")
    private Set<DriverQualification> qualifications;

    @OneToMany(mappedBy = "driver")
    private List<Transport> transports;
}
