package com.example.discordclone.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "group_titles")
public class GroupTitle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Group group;

    private String name;
    private int spamIntervalSeconds = 0;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<Permission> permissions = new HashSet<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Group getGroup() { return group; }
    public void setGroup(Group group) { this.group = group; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getSpamIntervalSeconds() { return spamIntervalSeconds; }
    public void setSpamIntervalSeconds(int spamIntervalSeconds) { this.spamIntervalSeconds = spamIntervalSeconds; }

    public Set<Permission> getPermissions() { return permissions; }
    public void setPermissions(Set<Permission> permissions) { this.permissions = permissions; }
}