package com.spring.aurora.service;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spring.aurora.dao.ContainerDao;
import com.spring.aurora.model.Container;

@Service("containerService")
@Transactional
public class ContainerServiceImpl implements ContainerService {

    @Autowired
    private ContainerDao containerDao;

    public void setContainerDao(ContainerDao containerDao) {
        this.containerDao = containerDao;
    }

    @Override
    public Container insert(Container container) {
        return this.containerDao.save(container);
    }

    @Override
    public Container update(Container container) {
        return this.containerDao.save(container);
    }

    @Override
    public Container delete(Container container) {
        this.containerDao.delete(container);
        return container;
    }

    @Override
    public List<Container> findAllByCustomerId(long customerId) {
        return this.containerDao.findAllByCustomerId(customerId);
    }

    @Override
    public List<Container> findAll() {
        List<Container> result = new ArrayList<>();
        this.containerDao.findAll().forEach(result::add);
        return result;
    }

    @Override
    public List<Container> findContainerActivityByDate(Date date) {
        return this.containerDao.findContainerActivityByDate(date);
    }

}
