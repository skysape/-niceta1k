package com.example.discordclone.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "groups")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String avatarUrl;

    @ManyToOne
    private User creator;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "group")
    private List<Channel> channels = new ArrayList<>(); // Max 50 text + 50 voice

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "group")
    private List<GroupTitle> titles = new ArrayList<>(); // Max 10

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public User getCreator() { return creator; }
    public void setCreator(User creator) { this.creator = creator; }

    public List<Channel> getChannels() { return channels; }
    public void setChannels(List<Channel> channels) { this.channels = channels; }

    public List<GroupTitle> getTitles() { return titles; }
    public void setTitles(List<GroupTitle> titles) { this.titles = titles; }
}