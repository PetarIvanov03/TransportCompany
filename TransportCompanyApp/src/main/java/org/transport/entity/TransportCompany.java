package org.transport.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

// Represents a transport company with employees, vehicles and financials
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransportCompany {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    private String address;

    private String contactInfo;

    @OneToMany(mappedBy = "company", cascade = CascadeType.PERSIST)
    private List<Employee> employees;

    @OneToMany(mappedBy = "company", cascade = CascadeType.PERSIST)
    private List<Vehicle> vehicles;
}
