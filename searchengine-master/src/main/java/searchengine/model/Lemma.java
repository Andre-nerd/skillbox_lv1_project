package searchengine.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;


@Entity
@Table(name = "lemma")
@Getter
@Setter
public class Lemma {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "site_id", referencedColumnName = "id")
    SiteModel owner;

    @Column(name = "lemma")
    private String lemma;

    @Column(name = "frequency")
    private int frequency;

    @OneToMany(mappedBy = "ownerLemma")
    private List<IndexModel> indexModels;
}

/** CREATE TABLE lemma(
 id INT PRIMARY KEY  NOT NULL AUTO_INCREMENT,
 site_id INT NOT NULL,
 lemma VARCHAR(255) NOT NULL,
 frequency INT NOT NULL,
 FOREIGN KEY (site_id) REFERENCES site (id) ON DELETE CASCADE
 ); */