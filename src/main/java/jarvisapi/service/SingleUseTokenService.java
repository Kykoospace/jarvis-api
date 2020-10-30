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

    public void delete(long id) throws SingleUseTokenNotFoundException, SingleUseTokenExpiredException {
        Optional<SingleUseToken> singleUseTokenOptional = this.singleUseTokenRepository.findById(id);

        if (!singleUseTokenOptional.isPresent()) {
            throw new SingleUseTokenNotFoundException();
        }

        this.singleUseTokenRepository.delete(singleUseTokenOptional.get());
    }

    /**
     * Check the single use token validity by his expiration date
     * @param singleUseToken
     * @return
     * @throws SingleUseTokenNotFoundException
     */
    public boolean isSingleUseTokenValid(SingleUseToken singleUseToken) throws SingleUseTokenNotFoundException {
        return !DateUtils.isDateExpired(singleUseToken.getExpirationDate());
    }

    public boolean isSingleUseTokenVerified(SingleUseToken singleUseToken, String token) {
        if (!singleUseToken.getToken().toString().equals(token)) {
            return false;
        }

        if (!this.isSingleUseTokenValid(singleUseToken)) {
            throw new SingleUseTokenExpiredException();
        }

        return true;
    }
}
