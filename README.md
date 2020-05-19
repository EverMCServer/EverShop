# EverShop

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/a9fb0d5ad5844f3098e80bc5fce01a2e)](https://app.codacy.com/gh/EverMCServer/EverShop?utm_source=github.com&utm_medium=referral&utm_content=EverMCServer/EverShop&utm_campaign=Badge_Grade_Dashboard)
[![GitHub Actions](https://github.com/EverMCServer/EverShop/workflows/GitHub%20Actions/badge.svg)](https://github.com/EverMCServer/EverShop/actions)

开发中，因为SignShop的Bug过多而且架构问题太大无法修复，所以重新开发一个商店插件

## TODO

  - [ ] tab completer (brigadier)
    - [ ] 框架
  - [ ] 收税。（最高税率应为10%， 因为钻石售价444， 收购价400）

## 如果你想测试

本体：可以在[github actions](https://github.com/EverMCServer/EverShop/actions)中下载最新的artifact， 或者在[maven](http://maven-djytw.azurewebsites.net/maven-repository/com/evermc/evershop/EverShop/1.0/)里下载。如果需要使用mysql， 需要下载带-Hikari的jar，只用sqlite的话下载另外一个小的就好。

依赖： Vault，以及一个经济插件（比如EssentialsX?）

可选依赖： WorldGuard LockettePro （可以配置受保护的箱子无法创建商店）

