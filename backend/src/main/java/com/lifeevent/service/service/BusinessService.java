package com.lifeevent.service.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifeevent.service.config.BusinessException;
import com.lifeevent.service.dto.BookCreateRequest;
import com.lifeevent.service.dto.PageResult;
import com.lifeevent.service.dto.PaymentCreateRequest;
import com.lifeevent.service.dto.RentCreateRequest;
import com.lifeevent.service.entity.*;
import com.lifeevent.service.mapper.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@Service
public class BusinessService {
    private final FamilyMapper familyMapper;
    private final DeceasedMapper deceasedMapper;
    private final GraveAreaMapper graveAreaMapper;
    private final GraveMapper graveMapper;
    private final RentRecordMapper rentRecordMapper;
    private final PaymentMapper paymentMapper;
    private final SacrificeBookMapper sacrificeBookMapper;
    private final StaffMapper staffMapper;
    private final JdbcTemplate jdbcTemplate;

    public BusinessService(FamilyMapper familyMapper, DeceasedMapper deceasedMapper, GraveAreaMapper graveAreaMapper,
                           GraveMapper graveMapper, RentRecordMapper rentRecordMapper, PaymentMapper paymentMapper,
                           SacrificeBookMapper sacrificeBookMapper, StaffMapper staffMapper, JdbcTemplate jdbcTemplate) {
        this.familyMapper = familyMapper;
        this.deceasedMapper = deceasedMapper;
        this.graveAreaMapper = graveAreaMapper;
        this.graveMapper = graveMapper;
        this.rentRecordMapper = rentRecordMapper;
        this.paymentMapper = paymentMapper;
        this.sacrificeBookMapper = sacrificeBookMapper;
        this.staffMapper = staffMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> dashboardSummary() {
        YearMonth month = YearMonth.now();
        return Map.of(
                "familyCount", count("select count(*) from family"),
                "deceasedCount", count("select count(*) from deceased"),
                "emptyGraveCount", count("select count(*) from grave where status='empty'"),
                "activeRentCount", count("select count(*) from rent_record where rent_status='active'"),
                "monthIncome", jdbcTemplate.queryForObject(
                        "select coalesce(sum(pay_amount),0) from payment where pay_time >= ? and pay_time < ?",
                        Object.class, month.atDay(1).atStartOfDay(), month.plusMonths(1).atDay(1).atStartOfDay()),
                "pendingBookCount", count("select count(*) from sacrifice_book where checkin_status='booked'")
        );
    }

    public List<Map<String, Object>> monthIncome() {
        return jdbcTemplate.queryForList("""
                select date_format(pay_time, '%Y-%m') incomeMonth, sum(pay_amount) totalIncome
                from payment
                group by date_format(pay_time, '%Y-%m')
                order by incomeMonth
                """);
    }

    public PageResult<Family> families(String keyword, long page, long size) {
        LambdaQueryWrapper<Family> wrapper = new LambdaQueryWrapper<>();
        if (hasText(keyword)) {
            wrapper.and(w -> w.like(Family::getFamilyName, keyword).or().like(Family::getPhone, keyword).or().like(Family::getIdCard, keyword));
        }
        wrapper.orderByDesc(Family::getFamilyId);
        Page<Family> p = familyMapper.selectPage(Page.of(page, size), wrapper);
        return new PageResult<>(p.getRecords(), p.getTotal(), page, size);
    }

    public void saveFamily(Family family) {
        if (family.getFamilyId() == null) {
            familyMapper.insert(family);
        } else {
            familyMapper.updateById(family);
        }
    }

    public void deleteFamily(Integer id) {
        Long related = deceasedMapper.selectCount(new LambdaQueryWrapper<Deceased>().eq(Deceased::getFamilyId, id));
        if (related > 0) {
            throw new BusinessException("该家属已有逝者档案，不能物理删除");
        }
        familyMapper.deleteById(id);
    }

    public PageResult<Map<String, Object>> deceased(String keyword, long page, long size) {
        String where = hasText(keyword) ? " where d.deceased_name like ? or f.family_name like ? or d.id_card like ? " : "";
        Object[] args = hasText(keyword) ? likeArgs(keyword, keyword, keyword) : new Object[]{};
        long total = count("select count(*) from deceased d join family f on d.family_id=f.family_id" + where, args);
        List<Map<String, Object>> rows = queryPage("""
                select d.*, f.family_name familyName
                from deceased d join family f on d.family_id=f.family_id
                %s order by d.deceased_id desc limit ? offset ?
                """.formatted(where), args, page, size);
        return new PageResult<>(rows, total, page, size);
    }

    public void saveDeceased(Deceased deceased) {
        mustExist(familyMapper.selectById(deceased.getFamilyId()), "家属不存在");
        if (deceased.getDeathDate().isBefore(deceased.getBirthDate())) {
            throw new BusinessException("离世日期不能早于出生日期");
        }
        if (deceased.getDeceasedId() == null) {
            deceasedMapper.insert(deceased);
        } else {
            deceasedMapper.updateById(deceased);
        }
    }

    public PageResult<GraveArea> graveAreas(String keyword, long page, long size) {
        LambdaQueryWrapper<GraveArea> wrapper = new LambdaQueryWrapper<>();
        if (hasText(keyword)) {
            wrapper.and(w -> w.like(GraveArea::getAreaCode, keyword).or().like(GraveArea::getAreaName, keyword));
        }
        wrapper.orderByDesc(GraveArea::getAreaId);
        Page<GraveArea> p = graveAreaMapper.selectPage(Page.of(page, size), wrapper);
        return new PageResult<>(p.getRecords(), p.getTotal(), page, size);
    }

    public void saveGraveArea(GraveArea area) {
        if (area.getAreaId() == null) {
            graveAreaMapper.insert(area);
        } else {
            graveAreaMapper.updateById(area);
        }
    }

    public PageResult<Map<String, Object>> graves(String keyword, long page, long size) {
        String where = hasText(keyword) ? " where g.grave_code like ? or ga.area_name like ? or g.status like ? " : "";
        Object[] args = hasText(keyword) ? likeArgs(keyword, keyword, keyword) : new Object[]{};
        long total = count("select count(*) from grave g join grave_area ga on g.area_id=ga.area_id" + where, args);
        List<Map<String, Object>> rows = queryPage("""
                select g.*, ga.area_name areaName
                from grave g join grave_area ga on g.area_id=ga.area_id
                %s order by g.grave_id desc limit ? offset ?
                """.formatted(where), args, page, size);
        return new PageResult<>(rows, total, page, size);
    }

    public void saveGrave(Grave grave) {
        mustExist(graveAreaMapper.selectById(grave.getAreaId()), "墓区不存在");
        if (grave.getGraveId() == null) {
            graveMapper.insert(grave);
        } else {
            graveMapper.updateById(grave);
        }
    }

    public List<Map<String, Object>> emptyGraves() {
        return jdbcTemplate.queryForList("""
                select g.grave_id graveId, g.grave_code graveCode, ga.area_name areaName,
                       g.location_desc locationDesc, g.rent_price rentPrice, g.max_years maxYears
                from grave g join grave_area ga on g.area_id=ga.area_id
                where g.status='empty' and ga.status='active'
                order by g.grave_code
                """);
    }

    public PageResult<Map<String, Object>> rents(String keyword, long page, long size) {
        String where = hasText(keyword) ? " where f.family_name like ? or d.deceased_name like ? or g.grave_code like ? " : "";
        Object[] args = hasText(keyword) ? likeArgs(keyword, keyword, keyword) : new Object[]{};
        long total = count(baseRentCountSql() + where, args);
        List<Map<String, Object>> rows = queryPage(baseRentSql() + where + " order by rr.rent_id desc limit ? offset ?", args, page, size);
        return new PageResult<>(rows, total, page, size);
    }

    @Transactional
    public void createRent(RentCreateRequest request) {
        mustExist(familyMapper.selectById(request.familyId()), "家属不存在");
        mustExist(deceasedMapper.selectById(request.deceasedId()), "逝者不存在");
        mustExist(staffMapper.selectById(request.staffId()), "经办员工不存在");
        Grave grave = graveMapper.selectById(request.graveId());
        mustExist(grave, "墓穴不存在");
        if (!"empty".equals(grave.getStatus())) {
            throw new BusinessException("只有空置墓穴可以办理租赁");
        }
        if (!request.expireDate().isAfter(request.startDate())) {
            throw new BusinessException("到期日期必须晚于开始日期");
        }
        RentRecord rent = new RentRecord();
        rent.setFamilyId(request.familyId());
        rent.setDeceasedId(request.deceasedId());
        rent.setGraveId(request.graveId());
        rent.setStaffId(request.staffId());
        rent.setStartDate(request.startDate());
        rent.setExpireDate(request.expireDate());
        rent.setTotalAmount(request.totalAmount());
        rent.setRentStatus("active");
        rentRecordMapper.insert(rent);
        grave.setStatus("rented");
        graveMapper.updateById(grave);
    }

    public void updateRent(Integer id, RentRecord rent) {
        rent.setRentId(id);
        rentRecordMapper.updateById(rent);
    }

    public List<Map<String, Object>> expiringRents() {
        return jdbcTemplate.queryForList(baseRentSql() + """
                where rr.rent_status='active' and rr.expire_date <= date_add(curdate(), interval 90 day)
                order by rr.expire_date
                """);
    }

    public PageResult<Map<String, Object>> payments(String keyword, long page, long size) {
        String where = hasText(keyword) ? " where p.invoice_no like ? or f.family_name like ? or p.pay_type like ? " : "";
        Object[] args = hasText(keyword) ? likeArgs(keyword, keyword, keyword) : new Object[]{};
        long total = count("""
                select count(*) from payment p
                join rent_record rr on p.rent_id=rr.rent_id
                join family f on rr.family_id=f.family_id
                """ + where, args);
        List<Map<String, Object>> rows = queryPage("""
                select p.*, f.family_name familyName, d.deceased_name deceasedName, g.grave_code graveCode, s.staff_name staffName
                from payment p
                join rent_record rr on p.rent_id=rr.rent_id
                join family f on rr.family_id=f.family_id
                join deceased d on rr.deceased_id=d.deceased_id
                join grave g on rr.grave_id=g.grave_id
                join staff s on p.staff_id=s.staff_id
                """ + where + " order by p.payment_id desc limit ? offset ?", args, page, size);
        return new PageResult<>(rows, total, page, size);
    }

    public void createPayment(PaymentCreateRequest request) {
        mustExist(rentRecordMapper.selectById(request.rentId()), "租赁订单不存在");
        mustExist(staffMapper.selectById(request.staffId()), "收款员工不存在");
        Payment payment = new Payment();
        payment.setRentId(request.rentId());
        payment.setStaffId(request.staffId());
        payment.setPayType(request.payType());
        payment.setPayMethod(request.payMethod());
        payment.setPayAmount(request.payAmount());
        payment.setPayTime(request.payTime());
        payment.setInvoiceNo(request.invoiceNo());
        payment.setRemark(request.remark());
        paymentMapper.insert(payment);
    }

    public PageResult<Map<String, Object>> sacrificeBooks(String keyword, long page, long size) {
        String where = hasText(keyword) ? " where f.family_name like ? or g.grave_code like ? or sb.checkin_status like ? " : "";
        Object[] args = hasText(keyword) ? likeArgs(keyword, keyword, keyword) : new Object[]{};
        long total = count("""
                select count(*) from sacrifice_book sb
                join family f on sb.family_id=f.family_id
                join grave g on sb.grave_id=g.grave_id
                """ + where, args);
        List<Map<String, Object>> rows = queryPage("""
                select sb.*, f.family_name familyName, g.grave_code graveCode, s.staff_name staffName
                from sacrifice_book sb
                join family f on sb.family_id=f.family_id
                join grave g on sb.grave_id=g.grave_id
                join staff s on sb.staff_id=s.staff_id
                """ + where + " order by sb.book_time desc limit ? offset ?", args, page, size);
        return new PageResult<>(rows, total, page, size);
    }

    public void createBook(BookCreateRequest request) {
        mustExist(familyMapper.selectById(request.familyId()), "家属不存在");
        mustExist(graveMapper.selectById(request.graveId()), "墓穴不存在");
        mustExist(staffMapper.selectById(request.staffId()), "登记员工不存在");
        SacrificeBook book = new SacrificeBook();
        book.setFamilyId(request.familyId());
        book.setGraveId(request.graveId());
        book.setStaffId(request.staffId());
        book.setBookTime(request.bookTime());
        book.setVisitorCount(request.visitorCount());
        book.setSpecialNeed(request.specialNeed());
        book.setCheckinStatus("booked");
        sacrificeBookMapper.insert(book);
    }

    public void checkinBook(Integer id) {
        SacrificeBook book = sacrificeBookMapper.selectById(id);
        mustExist(book, "预约不存在");
        if (!"booked".equals(book.getCheckinStatus())) {
            throw new BusinessException("只有待预约状态可以核销");
        }
        book.setCheckinStatus("checked_in");
        book.setCheckinTime(LocalDateTime.now());
        sacrificeBookMapper.updateById(book);
    }

    public void cancelBook(Integer id) {
        SacrificeBook book = sacrificeBookMapper.selectById(id);
        mustExist(book, "预约不存在");
        if ("checked_in".equals(book.getCheckinStatus())) {
            throw new BusinessException("已核销预约不能取消");
        }
        book.setCheckinStatus("cancelled");
        sacrificeBookMapper.updateById(book);
    }

    public List<Staff> staff() {
        return staffMapper.selectList(new LambdaQueryWrapper<Staff>().eq(Staff::getStatus, "active").orderByAsc(Staff::getStaffId));
    }

    private long count(String sql, Object... args) {
        Long value = jdbcTemplate.queryForObject(sql, Long.class, args);
        return value == null ? 0 : value;
    }

    private List<Map<String, Object>> queryPage(String sql, Object[] args, long page, long size) {
        Object[] pageArgs = new Object[args.length + 2];
        System.arraycopy(args, 0, pageArgs, 0, args.length);
        pageArgs[args.length] = size;
        pageArgs[args.length + 1] = (page - 1) * size;
        return jdbcTemplate.queryForList(sql, pageArgs);
    }

    private Object[] likeArgs(String... values) {
        Object[] args = new Object[values.length];
        for (int i = 0; i < values.length; i++) {
            args[i] = "%" + values[i] + "%";
        }
        return args;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private void mustExist(Object entity, String message) {
        if (entity == null) {
            throw new BusinessException(message);
        }
    }

    private String baseRentCountSql() {
        return """
                select count(*) from rent_record rr
                join family f on rr.family_id=f.family_id
                join deceased d on rr.deceased_id=d.deceased_id
                join grave g on rr.grave_id=g.grave_id
                join staff s on rr.staff_id=s.staff_id
                """;
    }

    private String baseRentSql() {
        return """
                select rr.*, f.family_name familyName, d.deceased_name deceasedName, g.grave_code graveCode, s.staff_name staffName
                from rent_record rr
                join family f on rr.family_id=f.family_id
                join deceased d on rr.deceased_id=d.deceased_id
                join grave g on rr.grave_id=g.grave_id
                join staff s on rr.staff_id=s.staff_id
                """;
    }
}

