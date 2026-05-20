package de.haushaltsbuch.backend.model;

import jakarta.persistence.*;
import lombok.*;


/**
 * A single condition within a {@link CategoryRule}.
 *
 * <p>All conditions of a rule are combined with logical AND: the rule matches
 * only if every one of its conditions evaluates to {@code true}.
 */
@Entity
@Table(name = "category_rule_conditions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class RuleCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The rule this condition belongs to. */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    private CategoryRule rule;

    /** Which transaction field is evaluated. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConditionField field;

    /** How the field value is compared to {@link #value}. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConditionMatcher matcher;

    /** The expected value to match against (case-insensitive). */
    @Column(nullable = false, length = 500)
    private String value;
}
