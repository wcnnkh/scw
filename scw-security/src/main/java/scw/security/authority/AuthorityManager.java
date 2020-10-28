package scw.security.authority;

import java.util.Collection;
import java.util.List;

import scw.aop.annotation.AopEnable;

@AopEnable(false)
public interface AuthorityManager<T extends Authority> {
	void register(T authority);
	
	T getAuthority(String id);

	/**
	 * 从全部中筛选结果
	 * @param authorityFilter
	 * @return
	 */
	List<T> getAuthorityList(AuthorityFilter<T> authorityFilter);
	
	/**
	 * 从根信息中筛选结果
	 * @param authorityFilter
	 * @return
	 */
	List<T> getRootList(AuthorityFilter<T> authorityFilter);
	
	/**
	 * 从全部中筛选结果，并返回树结构
	 * @param authorityFilter
	 * @return
	 */
	List<AuthorityTree<T>> getAuthorityTreeList(AuthorityFilter<T> authorityFilter);

	/**
	 * 从子集中筛选结果
	 * @param id
	 * @param authorityFilter
	 * @return
	 */
	List<T> getAuthoritySubList(String id, AuthorityFilter<T> authorityFilter);
	
	/**
	 * 从父级列表筛选结果
	 * @param id
	 * @param authorityFilter
	 * @return
	 */
	List<T> getParentList(String id, AuthorityFilter<T> authorityFilter);

	/**
	 * 从子集中筛选结果，并返回树结构
	 * @param id
	 * @param authorityFilter
	 * @return
	 */
	List<AuthorityTree<T>> getAuthoritySubTreeList(String id, AuthorityFilter<T> authorityFilter);

	/**
	 * 获取关联的结果，并组成树结构
	 * @param ids
	 * @param authorityFilter
	 * @return
	 */
	List<AuthorityTree<T>> getRelationAuthorityTreeList(Collection<String> ids, AuthorityFilter<T> authorityFilter);
	
	/**
	 * 获取关联的结果
	 * @param ids
	 * @param authorityFilter
	 * @return
	 */
	List<T> getRelationAuthorityList(Collection<String> ids, AuthorityFilter<T> authorityFilter);
}