package com.neuq.ccpcbackend;

import com.neuq.ccpcbackend.entity.School;
import com.neuq.ccpcbackend.entity.User;
import com.neuq.ccpcbackend.entity.UserRole;
import com.neuq.ccpcbackend.mapper.SchoolMapper;
import com.neuq.ccpcbackend.mapper.UserMapper;
import com.neuq.ccpcbackend.mapper.UserRoleMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@SpringBootTest
class CcpcBackendApplicationTests {

    @Resource
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private SchoolMapper schoolMapper;
    @Autowired
    private UserRoleMapper userRoleMapper;
    
    void generateJWT() {
        List<String> schoolNames = new ArrayList<>();
        List<String> schoolNameEns = new ArrayList<>();
        schoolNames.add("清华大学");
        schoolNameEns.add("Tsinghua University");

        schoolNames.add("北京大学");
        schoolNameEns.add("Beijing University");

        schoolNames.add("东北大学");
        schoolNameEns.add("Northeastern University");

        schoolNames.add("杭州电子科技大学");
        schoolNameEns.add("Hangzhou Dianzi University");

        long phoneNow = 13345678911L;

        for (int i = 0; i < schoolNames.size(); i++) {
            School school = new School();
            school.setId(UUID.randomUUID().toString());
            school.setName(schoolNames.get(i));
            school.setNameEn(schoolNameEns.get(i));
            school.setMailingAddress("河北省秦皇岛市海港区泰山路");
            school.setInstitution(schoolNames.get(i));
            school.setTaxpayerCode(generateDigitNumber(18));
            school.setInvoiceAddress("河北省秦皇岛市海港区泰山路");
            school.setInvoicePhone("13345678910");
            school.setBankName("中国银行");
            school.setBankCardCode(generateDigitNumber(19));
            school.setSchoolBadgeUrl("https://th.bing.com/th/id/OIP.45u8tASVBSOK3lQKrK4YqwHaHa?rs=1&pid=ImgDetMain");
            schoolMapper.insert(school);

            for (int j = 1; j <= 3; j++) {
                User user = new User();
                user.setId(UUID.randomUUID().toString());
                user.setPhone(String.valueOf(phoneNow++));
                user.setPassword(passwordEncoder.encode("ccpc2025"));
                user.setSchoolId(school.getId());
                user.setSex(true);
                user.setEmail(user.getPhone()+"@163.com");
                user.setFirstName(school.getName().substring(0, 1));
                user.setLastName("教练"+ j);
                user.setFirstNameEn(school.getNameEn());
                user.setLastNameEn("jiaolian"+ j);
                user.setAddress("河北省秦皇岛市海港区泰山路");
                user.setAddressee(user.getFirstName());
                user.setAddressPhone(user.getPhone());
                user.setClothSize("XL");
                userMapper.insert(user);

                UserRole userRole1 = new UserRole();
                userRole1.setId(UUID.randomUUID().toString());
                userRole1.setUserId(user.getId());
                userRole1.setRoleId("1");
                userRoleMapper.insert(userRole1);

                UserRole userRole2 = new UserRole();
                userRole2.setId(UUID.randomUUID().toString());
                userRole2.setUserId(user.getId());
                userRole2.setRoleId("6");
                userRoleMapper.insert(userRole2);
            }
        }
    }
    public static String generateDigitNumber(int len) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(len);
        sb.append(random.nextInt(9) + 1);
        for (int i = 0; i < len-1; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

}
