package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
public class DishServiceImpl implements DishService {
    @Autowired
    DishMapper dishMapper;
    @Autowired
    DishFlavorMapper dishFlavorMapper;
    @Autowired
    SetmealMapper setmealMapper;
    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish=new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.insert(dish);
        //insert 后生成的主键id
        Long id=dish.getId();
        List<DishFlavor> dishFlavorList=dishDTO.getFlavors();
        if(dishFlavorList!=null&&dishFlavorList.size()>0){
            for(DishFlavor df:dishFlavorList){
                df.setDishId(id);
            }
            dishFlavorMapper.insertBatch(dishFlavorList);
        }
    }

    @Override
    public PageResult page(DishPageQueryDTO dishDTO) {
        PageHelper.startPage(dishDTO.getPage(),dishDTO.getPageSize());
        Page<DishVO> page=dishMapper.pageQuery(dishDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    @Override
    //2条以上sql就要启用事务管理原子性
    @Transactional
    public void deleteBatch(List<Long> ids) {
        for(Long id:ids){
            Dish dish=dishMapper.getById(id);
            if(dish.getStatus()== StatusConstant.ENABLE)
                throw new DeletionNotAllowedException("售卖状态不允许删除");
        }
        List<Long> mealids=setmealMapper.getDish(ids);
        if(mealids!=null||mealids.size()>0)
            throw new DeletionNotAllowedException("有套餐关联不允许删除");
        dishFlavorMapper.deleteBatch(ids);
        dishMapper.deleteBatch(ids);
    }

    @Override
    public DishVO getDishId(Long id) {
        DishVO dishVO=new DishVO();
        Dish dish=dishMapper.getById(id);
        List<DishFlavor> dishFlavor=dishFlavorMapper.getByDishId(id);
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(dishFlavor);
        return dishVO;
    }

    @Override
    @Transactional
    public void updateWithFlavor(DishDTO dishDTO) {
        Dish dish=new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.update(dish);

        List<Long> dishId=new ArrayList<>();
        dishId.add(dish.getId());
        dishFlavorMapper.deleteBatch(dishId);

        List<DishFlavor> dishFlavorList=dishDTO.getFlavors();
        if(dishFlavorList==null|| dishFlavorList.isEmpty()) return ;
        for(DishFlavor dishFlavor:dishFlavorList){
            dishFlavor.setDishId(dish.getId());
        }
        dishFlavorMapper.insertBatch(dishFlavorList);
    }

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }
}
