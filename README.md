# EverShop

开发中，因为SignShop的Bug过多而且架构问题太大无法修复，所以重新开发一个商店插件

## 目标

- [ ] 完成所有SignShop具有的功能
  - [x] buy
  - [x] sell
  - [x] ibuy
  - [x] isell
- [x] 使用MySQL/SQLite存储商店信息
- [x] 更加容易输入的牌子格式
- [x] 可以链接按钮
- [ ] 可以设定开启时长
- [x] 可以>2箱子的以物易物
- [x] 可以（物+钱）易物
- [ ] 收税。（最高税率应为10%， 因为钻石售价444， 收购价400）
- [ ] hook wg， 防止链接他人箱子
- [ ] 限制牌子加入限制玩家和用户组功能，改为命令实现
- [x] 可以跨箱子购买物品（需要购买AB，箱子1有A, 箱子2有B，可以直接购买，signshop无法购买）

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
 /es set [shopid] permission type [none/blacklist/whitelist] 设置使用权限类型(evershop.set.perm/evershop.admin.perm)
 /es set [shopid] permission allow u:<username>/g:<groupname> 设置白名单用户/组(evershop.set.perm/evershop.admin.perm)
 /es set [shopid] permission deny u:<username>/g:<groupname> 设置黑名单用户/组(evershop.set.perm/evershop.admin.perm)
 /es set [shopid] text [1-4] [text]  设置牌子显示内容(evershop.set.text) {注意检测第一行内容}
 /es set [shopid] price [price]  设置商店价格(evershop.set.price) {注意修改牌子内容}
 /es set [shopid] time [time]  设置红石开启时间，只能用于device牌子(evershop.set.time)
 /es advanced 切换高级模式（高级功能比如交易商店需要）(evershop.advance)
 /es inspect 切换查看模式 （点击牌子不会交易，只查看信息）(evershop.inspect)
```

### 商店流程（防bug)

* 所有商店数据存数据库
* 箱子破坏，撤销商店
* 放置牌子，检测位置上是否有未删除的商店? -> 增加修改牌子信息方法， setline?
* 防止箱子，检测位置上是否有未删除的箱子
* 牌子破坏，撤销商店
