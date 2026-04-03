package ru.practicum.account.domain.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "Account")
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true)
    String username;

    @Column(name = "full_name")
    String fullName;

    @Column(name = "date_of_birth")
    LocalDate dateOfBirth;

    @Column(precision = 19, scale = 2, nullable = false)
    BigDecimal balance;
}
