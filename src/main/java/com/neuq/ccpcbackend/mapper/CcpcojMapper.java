package com.neuq.ccpcbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neuq.ccpcbackend.entity.Ccpcoj;
import com.neuq.ccpcbackend.entity.CcpcojAccount;
import com.neuq.ccpcbackend.vo.CcpcojAccountsVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CcpcojMapper extends BaseMapper<Ccpcoj> {
    CcpcojAccountsVo getCcpcojAccount(CcpcojAccount account, String area);
}
