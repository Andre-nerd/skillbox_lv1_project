package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.Lemma;
import searchengine.model.PageModel;
import searchengine.model.SiteModel;

import java.util.List;

public interface LemmaRepository extends JpaRepository<Lemma,Integer> {

    List<Lemma> findByLemma(String lemma);
}
