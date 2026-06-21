# 听书项目后端

## 项目介绍

本项目是一个基于 Spring Boot + Spring Cloud Alibaba 的听书平台后端项目，采用 Maven 多模块结构，包含网关、用户、专辑、订单、支付、搜索等服务模块。

## 技术栈

- Spring Boot
- Spring Cloud Alibaba
- Nacos
- OpenFeign
- MyBatis-Plus
- MySQL
- Redis
- RabbitMQ
- Docker

## 模块说明

- common：公共工具类、统一返回结果、异常处理
- model：实体类、DTO、VO
- server-gateway：网关服务
- service-client：Feign 远程调用接口
- service-album：专辑服务，负责分类、专辑、声音管理
- service-user：用户服务
- service-order：订单服务
- service-payment：支付服务
- service-search：搜索服务

## 已完成功能

- 专辑分类查询接口
- Nacos 配置中心接入
- MySQL 数据源配置
- 基础微服务模块搭建
