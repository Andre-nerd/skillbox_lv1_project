package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.SiteModel;
import searchengine.repositories.PageModelRepository;
import searchengine.repositories.SiteModelRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class IndexingServiceImpl implements IndexingService{

    private final SiteModelRepository siteModelRepository;
    private final PageModelRepository pageModelRepository;


    @Override
    public void clearDataBase(String root) {
        List<SiteModel> siteModelList = siteModelRepository.findByName(root);
        siteModelList.forEach(s -> siteModelRepository.delete(s));
    }

    @Override
    public void createNewRowIndexing(String root) {

    }

    @Override
    public void goAllPages(String root) {

    }

    @Override
    public void updateStatusTime(String root) {

    }

    @Override
    public void indexFinished(String root) {

    }

    @Override
    public void indexingFailed(String root) {

    }
}
