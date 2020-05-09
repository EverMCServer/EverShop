# EverShop

开发中，因为SignShop的Bug过多而且架构问题太大无法修复，所以重新开发一个商店插件

# TODO

- [ ] tab completer (brigadier)
  - [ ] 框架
- [ ] 移植fabric
- [ ] 收税。（最高税率应为10%， 因为钻石售价444， 收购价400）

## 如果你想测试

本体：可以在[github actions](https://github.com/EverMCServer/EverShop/actions)中下载最新的artifact， 或者在[maven](http://maven-djytw.azurewebsites.net/maven-repository/com/evermc/evershop/EverShop/1.0/)里下载。如果需要使用mysql， 需要下载带-Hikari的jar，只用sqlite的话下载另外一个小的就好。

依赖： Vault，以及一个经济插件（比如EssentialsX?）

## 详细

### 格式

暂定：只使用牌子第一行，剩下的为自定义内容

牌子类型通过定义的字符串从前往后匹配，价格从后往前匹配数字

```
buy - $100
出售  ￥100
buy 100
卖海晶灯一个100
```

均表示100单价的购买商店 

```
trade -100
交易  100
```

暂定： （物+钱）易物， 不写金额就是普通以物易物， 负数价格 表示 使用者出物，所有者出物+钱； 正数价格 表示 使用者出物+钱， 所有者出物

交易商店链接多个箱子的方法：左键链接所有者物品（输出）箱， 右键链接存储使用者物品（输入）箱？

```
device $100
开启 $100
```

并通过指令设置开启时长

```
dispose
```
如果不链接箱子则是普通销毁牌子（同signshop）
最多可链接一个箱子，销毁的东西自动进入箱子，如果箱子满了自动丢弃最早进入的物品，可防止手残

------

表示商店激活状态： 第一行变色加粗

黑色： 未创建

蓝色： 正常

红色： 缺货

### 指令

/evershop 或 /es

```
 /es reload  重载(evershop.admin.op)
 /es list [name or uuid] 查看商店列表(evershop.list/evershop.list.others)
 /es info 查看视线指向商店信息(evershop.info/evershop.info.others)
 /es info shopid 查看指定商店信息(evershop.info/evershop.info.others)
 /es log [shopid] 查看购买记录(evershop.info/evershop.info.others)
 /es set [shopid] permission type [none/blacklist/whitelist] 设置使用权限类型(evershop.set.perm/evershop.admin)
 /es set [shopid] permission add u:<username>/g:<groupname> 添加用户/组到名单(evershop.set.perm/evershop.admin)
 /es set [shopid] permission remove u:<username>/g:<groupname> 移除用户/组(evershop.set.perm/evershop.admin)
 /es set [shopid] text [1-4] [text]  设置牌子显示内容(evershop.set.text/evershop.admin) {注意检测第一行内容}
 /es set [shopid] price [price]  设置商店价格(evershop.set.price/evershop.admin) {注意修改牌子内容}
 /es set [shopid] duration [time]  设置红石开启时间，只能用于device牌子(evershop.set.duration/evershop.admin)
 /es advanced 切换高级模式（高级功能比如交易商店需要）(evershop.advance)
 /es inspect 切换查看模式 （点击牌子不会交易，只查看信息）(evershop.inspect)
 /es help 帮助（与/es相同）
 /es selection 查看当前选中的方块位置
 /es clear 清除选择
```

### 权限

玩家权限

```
evershop             基本权限。显示帮助、链接物品
evershop.advanced    切换高级模式
evershop.info        查看商店信息
evershop.inspect     切换inspect模式
evershop.list        自己的商店列表
evershop.set         商店设置参数权限
evershop.set.perm    设置使用权限
evershop.set.text    修改牌子文字
evershop.set.price   修改价格
evershop.set.duration    设置红石开启时长
evershop.create.[type] 创建type类型商店
evershop.multiworld  创建跨世界商店
```

管理权限

```
evershop.admin.op       op
evershop.admin.remove   强制删除商店
evershop.info.others    查看他人商店信息
evershop.list.others    查看他人商店列表
evershop.set.admin      设置他人商店参数
```

### 商店流程（防bug)

* 所有商店数据存数据库
* 箱子破坏，撤销商店
* 放置牌子，检测位置上是否有未删除的商店? -> 增加修改牌子信息方法， setline?
* 牌子破坏，撤销商店
* 启动时检测所有sign可用性
