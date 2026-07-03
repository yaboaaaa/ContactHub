package com.example.addressbook.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.util.StringUtils;

public class ContactDTO {

    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名长度不能超过50个字符")
    private String name;

    private Integer gender;

    @Pattern(regexp = "^\\d{6,20}$", message = "手机号格式不正确")
    private String phoneMobile;

    private String phoneHome;

    private String phoneWork;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

    @Size(max = 100, message = "公司长度不能超过100个字符")
    private String company;

    @Size(max = 50, message = "职位长度不能超过50个字符")
    private String jobTitle;

    @Size(max = 30, message = "省份长度不能超过30个字符")
    private String province;

    @Size(max = 30, message = "城市长度不能超过30个字符")
    private String city;

    @Size(max = 30, message = "区/县长度不能超过30个字符")
    private String district;

    @Size(max = 200, message = "详细地址长度不能超过200个字符")
    private String addressDetail;

    private String birthday;

    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String notes;

    private Long groupId;

    @AssertTrue(message = "至少需要填写一个联系电话")
    public boolean isValidPhone() {
        return StringUtils.hasText(phoneMobile)
                || StringUtils.hasText(phoneHome)
                || StringUtils.hasText(phoneWork);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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