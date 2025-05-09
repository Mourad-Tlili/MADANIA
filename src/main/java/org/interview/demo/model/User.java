package org.interview.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import java.time.LocalDate; // Import LocalDate

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true) // Assuming CIN string itself is still unique for a person
    private String cin;

    @Column(nullable = false)
    private LocalDate cinReleaseDate; // New field for CIN release date

    @Column(nullable = false)
    private boolean isMarried;

    public boolean isMarried() {
        return isMarried;
    }
}
