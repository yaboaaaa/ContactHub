package com.yabo.addressbook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "联系人创建/更新请求体")
public class ContactDTO {

    @Size(max = 50, message = "姓名长度不能超过50个字符")
    @Schema(description = "显示名（完整姓名）。若未提供姓/名，后端会自动拆分/拼接", example = "张三")
    private String name;

    @NotBlank(message = "姓不能为空")
    @Size(max = 30, message = "姓长度不能超过30个字符")
    @Schema(description = "姓", example = "张")
    private String familyName;

    @NotBlank(message = "名不能为空")
    @Size(max = 30, message = "名长度不能超过30个字符")
    @Schema(description = "名", example = "三")
    private String givenName;

    @Schema(description = "性别: 0-不便透露, 1-男, 2-女", example = "1")
    private Integer gender;

    @Pattern(regexp = "^(\\d{6,20})?$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13800138000")
    private String phoneMobile;

    @Schema(description = "家庭电话", example = "010-12345678")
    private String phoneHome;

    @Schema(description = "工作电话", example = "021-87654321")
    private String phoneWork;

    @Email(regexp = ".*", message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    @Size(max = 100, message = "公司长度不能超过100个字符")
    @Schema(description = "公司名称", example = "XX科技有限公司")
    private String company;

    @Size(max = 50, message = "职位长度不能超过50个字符")
    @Schema(description = "职位", example = "软件工程师")
    private String jobTitle;

    @Size(max = 30, message = "省份长度不能超过30个字符")
    @Schema(description = "省份", example = "广东省")
    private String province;

    @Size(max = 30, message = "城市长度不能超过30个字符")
    @Schema(description = "城市", example = "深圳市")
    private String city;

    @Size(max = 30, message = "区/县长度不能超过30个字符")
    @Schema(description = "区/县", example = "南山区")
    private String district;

    @Size(max = 200, message = "详细地址长度不能超过200个字符")
    @Schema(description = "详细地址", example = "XX路XX号XX室")
    private String addressDetail;

    @Schema(description = "生日 (yyyy-MM-dd)", example = "1990-01-01")
    private String birthday;

    @Size(max = 500, message = "备注长度不能超过500个字符")
    @Schema(description = "备注", example = "老同学")
    private String notes;

    @Schema(description = "分组ID", example = "1")
    private Long groupId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public String getPhoneMobile() {
        return phoneMobile;
    }

    public void setPhoneMobile(String phoneMobile) {
        this.phoneMobile = phoneMobile;
    }

    public String getPhoneHome() {
        return phoneHome;
    }

    public void setPhoneHome(String phoneHome) {
        this.phoneHome = phoneHome;
    }

    public String getPhoneWork() {
        return phoneWork;
    }

    public void setPhoneWork(String phoneWork) {
        this.phoneWork = phoneWork;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getAddressDetail() {
        return addressDetail;
    }

    public void setAddressDetail(String addressDetail) {
        this.addressDetail = addressDetail;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }
}