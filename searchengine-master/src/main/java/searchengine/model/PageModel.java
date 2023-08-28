package searchengine.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "page")
@Getter
@Setter
@ToString
public class PageModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "site_id", referencedColumnName = "id")
    SiteModel owner;

    @Column(name = "path")
    private String path;

    @Column(name ="code")
    private int code;

    @Column(name = "content")
    private String content;

    @OneToMany(mappedBy = "ownerPage")
    private List<IndexModel> indexes;
}
/** create table page(
 id int PRIMARY KEY  NOT NULL AUTO_INCREMENT,
 site_id int NOT NULL,
 `path` TEXT NOT NULL,
 `code` INT NOT NULL,
 content MEDIUMTEXT NOT NULL,
 FOREIGN KEY (site_id) REFERENCES site (id) ON DELETE CASCADE
 );*/
