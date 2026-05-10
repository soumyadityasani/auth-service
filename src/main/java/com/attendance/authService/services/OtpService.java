package com.attendance.authService.services;

import com.attendance.authService.entity.PendingOtp;
import com.attendance.authService.exceptions.OtpResponseException;
import com.attendance.authService.repo.PendingOtpRepo;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {

    @Value("${twilio.phone.number}")
    private String fromPhone;

    @Autowired
    private PendingOtpRepo pendingOtpRepo;

    private final RedisTemplate<String, Object> redisTemplate;

    private static final long OTP_EXPIRATION = 5; // minutes

    public OtpService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean sendOtp(String contact) {

        //STATUS
        boolean isSentOtp=false;

        //RE-VERIFICATION
        if(pendingOtpRepo.existsByContact(contact)){
            pendingOtpRepo.deleteById(contact);
        }

        //GENERATE OTP
        String otp = String.valueOf(100000 + new Random().nextInt(900000));

        //CALCULATE EXPIRY
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(15);

        try {

             //CREATE THE MESSAG
             Message message=Message.creator(
                    new PhoneNumber("+91"+contact),
                    new PhoneNumber(fromPhone),
                    "Your OTP is: " + otp
            ).create();

             //CHECKING
             if(message!=null && message.getSid()!=null){
                 pendingOtpRepo.save(new PendingOtp(contact, otp, expiry));

                 isSentOtp=true;
             }

            return isSentOtp;

        } catch (Exception e) {

            e.printStackTrace();

            return false;
        }
    }

    public boolean verifyOtp(String contact, String otp) {

        //FETCHING THE CONTACT INF
        PendingOtp pendingOtp = pendingOtpRepo.findById(contact).orElse(null);

        //CHECKING OTP
        if (pendingOtp != null && pendingOtp.getOtp().equals(otp) && pendingOtp.getExpiry().isAfter(LocalDateTime.now())) {

            //DELETE THE CONTACT INF
            pendingOtpRepo.deleteById(contact);

            return true;
        }
        return false;
    }



    // ✅ Generate + Store OTP in Redis
    public String generateAndSaveOtp(String email) {
        String otp = String.valueOf(new Random().nextInt(900000) + 100000);

        String key = "OTP:EMAIL:" + email;

        redisTemplate.opsForValue().set(key, otp, OTP_EXPIRATION, TimeUnit.MINUTES);

        return otp;
    }

    // ✅ Validate OTP
    public void validateOtp(String email, String otp) {

        String key = "OTP:EMAIL:" + email;

        String storedOtp = (String) redisTemplate.opsForValue().get(key);

        if (storedOtp == null) {
            throw new OtpResponseException("EXPIRED OTP");
        }

        if (!storedOtp.equals(otp)) {
            throw new OtpResponseException("INVALID OTP");
        }

        // ✅ Delete after success (VERY IMPORTANT)
        redisTemplate.delete(key);
    }
}

