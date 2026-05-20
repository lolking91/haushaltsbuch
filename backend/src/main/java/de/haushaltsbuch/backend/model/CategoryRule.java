package de.haushaltsbuch.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


/**
 * A rule that automatically assigns a category to matching transactions.
 *
 * <p>A rule consists of one or more {@link RuleCondition conditions}, all of
 * which must match (logical AND). When multiple rules match the same transaction,
 * the one with the highest {@link #priority} wins.
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

    /** All conditions that must match for this rule to fire. */
    @OneToMany(mappedBy = "rule", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RuleCondition> conditions = new ArrayList<>();
}
