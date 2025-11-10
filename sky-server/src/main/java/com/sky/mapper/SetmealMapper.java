package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishItemVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);

    List<Long> getDish(List<Long> ids);

    /**
     * 动态条件查询套餐
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据套餐id查询菜品选项
     * @param setmealId
     * @return
     */
    @Select("select sd.name, sd.copies, d.image, d.description " +
            "from setmeal_dish sd left join dish d on sd.dish_id = d.id " +
            "where sd.setmeal_id = #{setmealId}")
    List<DishItemVO> getDishItemBySetmealId(Long setmealId);
    @Insert("insert into setmeal (name, price, status, description, image) " +
            "values (#{name}, #{price}, #{status}, #{description}, #{image})")
    @AutoFill(value = OperationType.INSERT)
    void insert(Setmeal setmeal);
    @Insert("insert into setmeal_dish(setmeal_id,dish_id,name,price,copies) values (#{setmealId},#{dishId},#{name},#{price},#{copies})")
    void insertMealdish(SetmealDish dish);


    void deleteids(List<Long> ids);
    @Select("select * from setmeal where id=#{id}")
    Setmeal getById(long id);
    @AutoFill(value = OperationType.UPDATE)
    void update(Setmeal setmealDTO);
    @Delete("delete from setmeal_dish where dish_id=#{id}")
    void deleteDish(Long id);
}
