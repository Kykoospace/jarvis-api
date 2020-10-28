package jarvisapi.service;

import jarvisapi.entity.SingleUseToken;
import jarvisapi.exception.SingleUseTokenExpiredException;
import jarvisapi.exception.SingleUseTokenNotFoundException;
import jarvisapi.repository.SingleUseTokenRepository;
import jarvisapi.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SingleUseTokenService {

    @Value("${spring.jwt.singleUseTokenExpiration}")
    private long SUT_EXPIRATION;

    @Autowired
    private SingleUseTokenRepository singleUseTokenRepository;

    public SingleUseToken create() {
        SingleUseToken singleUseToken = new SingleUseToken(DateUtils.getExpirationDate(SUT_EXPIRATION));

        return this.singleUseTokenRepository.save(singleUseToken);
    }

    public SingleUseToken get(long id) {
        Optional<SingleUseToken> singleUseToken = this.singleUseTokenRepository.findById(id);

        if (!singleUseToken.isPresent()) {
            throw new SingleUseTokenNotFoundException();
        } else if (DateUtils.isDateExpired(singleUseToken.get().getExpirationDate())) {
            throw new SingleUseTokenExpiredException();
        }

        return singleUseToken.get();
    }

    public void remove(long id) throws SingleUseTokenNotFoundException, SingleUseTokenExpiredException {
        SingleUseToken singleUseToken = this.get(id);
        this.singleUseTokenRepository.delete(singleUseToken);
    }

    /**
     * Check the single use token validity by his expiration date
     * @param id
     * @return
     * @throws SingleUseTokenNotFoundException
     */
    public boolean isSingleUseTokenValid(long id) throws SingleUseTokenNotFoundException {
        Optional<SingleUseToken> singleUseTokenOptional = this.singleUseTokenRepository.findById(id);

        if (!singleUseTokenOptional.isPresent()) {
            throw new SingleUseTokenNotFoundException();
        }

        SingleUseToken singleUseToken = singleUseTokenOptional.get();

        return !DateUtils.isDateExpired(singleUseToken.getExpirationDate());
    }
}
