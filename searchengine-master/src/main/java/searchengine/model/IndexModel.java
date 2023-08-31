package searchengine.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Table(name = "`index`")
@Getter
@Setter
public class IndexModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "page_id", referencedColumnName = "id")
    PageModel ownerPage;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "lemma_id", referencedColumnName = "id")
    Lemma ownerLemma;

    @Column(name = "`rank`")
    private int rank;
}

/** CREATE TABLE `index` (
 id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
 page_id INT NOT NULL,
 lemma_id INT NOT NULL,
 `rank` FLOAT NOT NULL,
 FOREIGN KEY (page_id) REFERENCES `page` (id) ON DELETE CASCADE,
 FOREIGN KEY (lemma_id) REFERENCES lemma (id) ON DELETE CASCADE
 ); */