package searchengine.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "site")
public class SiteModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Enumerated(EnumType.STRING) /** For Enum */
    @Column(name = "status",columnDefinition = "enum")
    private SiteStatus status;

    @Column(name = "status_time")
    private LocalDateTime status_time;

    @Column(name = "last_error")
    private String last_error;

    @Column(name = "url")
    private String url;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "owner")
    private List<PageModel> pages;

    @OneToMany(mappedBy = "owner")
    private List<Lemma> lemmas;
}
