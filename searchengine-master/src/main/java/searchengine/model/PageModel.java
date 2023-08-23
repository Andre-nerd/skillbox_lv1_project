package searchengine.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

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
}
