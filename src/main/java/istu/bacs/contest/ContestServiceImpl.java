package istu.bacs.contest;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContestServiceImpl implements ContestService {

    private final ContestRepository contestRepository;

    public ContestServiceImpl(ContestRepository contestRepository) {
        this.contestRepository = contestRepository;
    }

    @Override
    public Contest findById(int contestId) {
        return contestRepository.findById(contestId).orElse(null);
    }

    @Override
    public List<Contest> findAll() {
        return contestRepository.findAll();
    }

    @Override
    public void save(Contest contest) {
        contestRepository.save(contest);
    }
}