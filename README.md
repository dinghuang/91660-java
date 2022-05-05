# 健康160全自动挂号脚本

## 功能介绍
- [x] 可以配置多个用户（一个用户关联一个医生）
- [x] 支持轮询重试
- [x] 支持邮件通知
- [x] 伪造浏览器客户端及IP防封
- [x] 结合selenium做登录（效率慢一点，不过官方一直会更改登录策略，用这个比较保险）



## 环境要求
- 需要java1.8及以上
- maven

到[网页](https://chromedriver.storage.googleapis.com/index.html)中下载对应chrome浏览器的版本对应的驱动，修改类``WebDriverConfiguration``的驱动路径

## 使用
运行脚本
```$shell
sh start.sh
```


> 本项目仅供学习交流，最初是因为老婆挂不到这边医院的号，都是黄牛在卖，迫不得已开发了一个这个，借鉴了项目（https://github.com/pengpan/91160）写了java版本的。我也十分讨厌黄牛奸商，故该脚本开源但禁止商业使用，望理解


