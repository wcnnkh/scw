package scw.lucene.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Field {
	/**
	 * 只有设置了索引属性为true，lucene才为这个域的Term词创建索引。<br/>
	 * 在实际的开发中，有一些字段是不需要创建索引的，比如商品的图片等。我们只需要对参与搜索的字段做索引处理。
	 * 
	 * @return
	 */
	boolean indexed() default true;

	/**
	 * 只有设置了存储属性为true，在查找的时候，才能从文档中获取这个域的值。<br/>
	 * 在实际开发中，有一些字段是不需要存储的。比如：商品的描述信息。<br/>
	 * 因为商品描述信息，通常都是大文本数据，读的时候会造成巨大的IO开销。而描述信息是不需要经常查询的字段，这样的话就白白浪费了cpu的资源了。<br/>
	 * 因此，像这种不需要经常查询，又是大文本的字段，通常不会存储到索引库
	 * 
	 * @return
	 */
	boolean stored() default true;

	/**
	 * 只有设置了分词属性为true，lucene才会对这个域进行分词处理。 在实际的开发中，有一些字段是不需要分词的，比如商品id，商品图片等。
	 * 而有一些字段是必须分词的，比如商品名称，描述信息等。
	 * 
	 * @return
	 */
	boolean tokenized() default false;
}
