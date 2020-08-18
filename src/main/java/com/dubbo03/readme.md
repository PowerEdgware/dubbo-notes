
###SPI机制###  
**1.位置**  
>META-INF/services  
>META-INF/dubbo  
>META-INF/dubbo/internal  
---

** 2.实现原理**  
### ExtensionLoader  
---
方法： `  public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type)`  
type接口必须具备：SPI注解  
根据type获取到一个ExtensionLoader实例，并缓存。key=type,value=ExtensionLoader    
---

** 获取指定名称扩展点： ** ` public T getExtension(String name)`   
加载所有此接口对应的文件中实现类，放入实例loader集合：cachedClasses，找到指定name的Class<?> clazz 
   * 实例化 clazz   
   clazz.newInstance() 放入全局类ExtensionLoader容器:key=clazz,value=instanceType   
   * 依赖注入：   
   使用objectFactory(自适应扩展点再说由来) 对type实例本身所有具set方法属性进行扩展点注入，注入的是自适应扩展点实例,类型是type实例set方法的入参类型  
  调用流程： `AdaptiveExtensionFactory.SpiExtensionFactory.loader.getAdaptiveExtension` 
 * 实例包装  
  type实例自身属性扩展点注入完毕，再对type实例进行包装，包装规则： 所有构造方法带有type类型的扩展点，会把type实例进行包装，创建包装扩展点的实例，并对包装的实例进行依赖注入  
调用流程： `instance = injectExtension((T) wrapperClass.getConstructor(type).newInstance(instance));`  
  * 默认扩展点的名字： type类型接口上注解SPI的value值  
  
** 获取默认的扩展点** `public T getDefaultExtension()`  
   * 默认的扩展点名称 `cachedDefaultName`  
   默认扩展点名称是根据扩展点接口type的注解SPI获取的，cachedDefaultName的值为：`@SPI`的value.代码位置：  
	`private void cacheDefaultExtensionName()`
 
** 获取自适应扩展点** `public T getAdaptiveExtension()`  
   每个ExtensionLoader 只保存了一个自适应扩展点的Class 属性名称：`cachedAdaptiveClass` 自适应Class依赖 `@Adaptove`注解  
   * 类级别的 `@Adaptove`   
   根据接口类型type 找到所有实现类Class ,如果某个Class 带有`@Adaptove` 注解， 则默认就是该type接口的 自适应扩展点实现。名称：Adaptive+type.getSimpleName  
   比如Compiler接口，他的自适应扩展点：AdaptiveCompiler
   
 * 方法级别的 `@Adaptove`  
 接口type存在方法级别的 `@Adaptove`注解，会生成动态代理类，名字：type.getSimpleName+$Adaptive  
 比如Protocol接口扩展点，存在方法级别的Adaptive注解，生成的代理类：Protocol$Adaptive 参见：com.adaptive包下的动态代理类。
 
 * 自适应扩展点实例创建  
获取到自适应扩展的类后, 调用无参构造器创建自适应扩展点实例，并实现依赖注入：
 `injectExtension((T) getAdaptiveExtensionClass().newInstance())`  
 自适应扩展点实例不会被包装
 
 note:一个type接口既没有类级别Adaptive也灭有方法级别Adaptive，则获取自适应扩展点会报错。
 
 ```
 No adaptive method exist on extension com.custome.IFilter, refuse to create the adaptive class!
 
 ```
 ** 激活扩展点**   
 使用的注解：`@Activate` 根据条件激活指定的扩展点
 

  
   
  




   











---
##MD的一些操作

`单行代码`

```
代码块
xxxxx
abcn

```

**无序列表**

+ a
- b
* c

**列表嵌套**
*  一级无序列表内容     
    * 二级

**分割线**  
---
----
***
*****

**引用**  
在引用的文字前加>即可。引用也可以嵌套，如加两个>>三个>>>  
> 这是引用的内容
> >这是引用的内容

**标题**   
*** 在想要设置为标题的文字前面加#来表示 ***
*** 一个#是一级标题，二个#是二级标题，以此类推。支持六级标题。* **

** 字体**

** 加粗**  
  * 斜体*  
~~ 删除线~~  




