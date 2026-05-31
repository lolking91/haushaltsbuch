package de.haushaltsbuch.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


/**
 * A rule that automatically assigns a category to matching transactions.
 *
 * <p>A rule consists of one or more {@link RuleCondition conditions} combined via
 * a {@link ConditionOperator}: either all conditions must match (AND) or at least
 * one must match (OR). When multiple rules match the same transaction, the one
 * with the highest {@link #priority} wins.
 */
@Entity
@Table(name = "category_rules")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = "conditions")
public class CategoryRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The category to assign when this rule matches. */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /** Optional human-readable label, e.g. {@code "Amazon → Shopping"}. */
    private String name;

    /**
     * Matching priority — higher value wins when multiple rules match.
     * Defaults to {@code 0}.
     */
    @Column(nullable = false)
    @Builder.Default
    private int priority = 0;

    /**
     * When {@code false} this rule is skipped during matching.
     * Defaults to {@code true}.
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    /**
     * How conditions are combined: {@link ConditionOperator#ALL} requires every
     * condition to match (AND); {@link ConditionOperator#ANY} requires at least one
     * to match (OR). Defaults to {@link ConditionOperator#ALL}.
     */
    @Column(nullable = false, length = 3)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ConditionOperator conditionOperator = ConditionOperator.ALL;

    /** Conditions evaluated against the transaction according to {@link #conditionOperator}. */
    @OneToMany(mappedBy = "rule", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RuleCondition> conditions = new ArrayList<>();
}
