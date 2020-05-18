# EverShop

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/a9fb0d5ad5844f3098e80bc5fce01a2e)](https://app.codacy.com/gh/EverMCServer/EverShop?utm_source=github.com&utm_medium=referral&utm_content=EverMCServer/EverShop&utm_campaign=Badge_Grade_Dashboard)
[![GitHub Actions](https://github.com/EverMCServer/EverShop/workflows/Java%20CI%20with%20Maven/badge.svg)](https://github.com/EverMCServer/EverShop/actions)

开发中，因为SignShop的Bug过多而且架构问题太大无法修复，所以重新开发一个商店插件

## TODO

  - [ ] tab completer (brigadier)
    - [ ] 框架
  - [ ] 收税。（最高税率应为10%， 因为钻石售价444， 收购价400）
  - [ ] 便利店（配合新版空岛）
    - [ ] 惩罚、重置时间设置
    - [ ] GUI
    - [ ] 玩家克隆便利店

## 如果你想测试

本体：可以在[github actions](https://github.com/EverMCServer/EverShop/actions)中下载最新的artifact， 或者在[maven](http://maven-djytw.azurewebsites.net/maven-repository/com/evermc/evershop/EverShop/1.0/)里下载。如果需要使用mysql， 需要下载带-Hikari的jar，只用sqlite的话下载另外一个小的就好。

依赖： Vault，以及一个经济插件（比如EssentialsX?）

可选依赖： WorldGuard LockettePro （可以配置受保护的箱子无法创建商店）

## 指令

/evershop 或 /es

| Command | Description | Permission |
|---------|-------------|------------|
| /es | 总命令 显示帮助 | evershop |
| /es advanced | 切换高级模式（高级功能比如交易商店需要）| evershop.advanced |
| /es clear | 清除选择 | evershop |
| /es help | 帮助（与/es相同） | evershop |
| /es info | 查看视线指向商店信息 | evershop.info |
| /es info &lt;shopid> | 查看指定商店信息 | evershop.info |
| /es list [name or uuid] | 查看商店列表 | evershop.list |
| /es log [shopid] | 查看购买记录 | evershop.info |
| /es reload | 重载配置文件 | evershop.admin.op |
| /es set [shopid] permission type [none/blacklist/whitelist] | 设置使用权限类型 | evershop.set.perm |
| /es set [shopid] permission add u:&lt;username>/g:&lt;groupname> | 添加用户/组到名单 | evershop.set.perm |
| /es set [shopid] permission remove u:&lt;username>/g:&lt;groupname> | 移除用户/组 | evershop.set.perm |
| /es set [shopid] text [1-4] [text] | 设置牌子显示内容 | evershop.set.text |
| /es set [shopid] price [price] | 设置商店价格 | evershop.set.price |
| /es set [shopid] duration [time] | 设置红石开启时间 | evershop.set.duration |
| /es slot [shopid] | 查看抽奖箱概率 | evershop.create.SLOT |
| /es slot &lt;shopid> set &lt;itemkey> &lt;possibility>| 设置抽奖箱概率 | evershop.create.SLOT |

## 权限

玩家权限

| Permission | Description | 
|------------|-------------|
| evershop                | 基本权限。显示帮助、链接物品  |
| evershop.advanced       | 切换高级模式                 |
| evershop.info           | 查看商店信息                 |
| evershop.inspect        | 切换inspect模式              |
| evershop.list           | 自己的商店列表               |
| evershop.set            | 商店设置参数权限             |
| evershop.set.perm       | 设置使用权限                 |
| evershop.set.text       | 修改牌子文字                 |
| evershop.set.price      | 修改价格                     |
| evershop.set.duration   | 设置红石开启时长             |
| evershop.create.[type]  | 创建type类型商店             |
| evershop.multiworld     | 创建跨世界商店               |

管理权限

| Permission | Description | 
|------------|-------------|                    
| evershop.admin.op       | OP（重载插件）   |
| evershop.admin.remove   | 强制删除商店     |
| evershop.info.others    | 查看他人商店信息 |
| evershop.list.others    | 查看他人商店列表 |
| evershop.set.admin      | 设置他人商店参数 |
