package com.yabo.addressbook.controller;

import com.yabo.addressbook.dto.ApiResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

@RestController
@RequestMapping("/api/i18n")
public class I18nApiController {

    private final MessageSource messageSource;

    public I18nApiController(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @GetMapping
    public ApiResult<Map<String, String>> getMessages(HttpServletRequest request) {
        Locale locale = request.getLocale();
        String[] keys = {
            "app.name", "app.tagline",
            "nav.contacts", "nav.recycle", "nav.admin", "nav.logout", "nav.avatar", "nav.contactsList", "nav.profile",
            "page.title.login", "page.title.register", "page.title.contacts", "page.title.recycle",
            "page.title.error", "page.title.admin", "page.title.admin.brand", "page.title.profile",
            "login.title", "login.username", "login.password", "login.remember", "login.captcha",
            "login.btn", "login.noAccount", "login.register",
            "login.placeholder.username", "login.placeholder.password", "login.placeholder.captcha",
            "login.error", "login.error.captcha", "login.error.remaining", "login.error.locked", "login.error.lockedShort",
            "login.logout", "login.registered",
            "register.title", "register.username", "register.password", "register.email", "register.email.placeholder",
            "register.captcha", "register.btn", "register.hasAccount", "register.login",
            "register.placeholder.username", "register.placeholder.password", "register.placeholder.captcha",
            "register.usernameAvailable", "register.usernameTaken", "register.usernameChecking",
            "register.pwdTooShort", "register.pwdWeak", "register.pwdMedium", "register.pwdGood", "register.pwdStrong",
            "contact.exportAll", "contact.addFailed", "contact.updateFailed", "contact.deleteFailed",
            "contact.loadingFailed", "contact.nameRequired", "contact.movedToRecycle",
            "contact.avatarUploaded", "contact.avatarUploadFailed", "contact.saveAvatarFirst",
            "contact.add", "contact.search", "contact.advanced", "contact.loading", "contact.noData",
            "contact.avatar", "contact.name", "contact.gender", "contact.phone", "contact.email",
            "contact.company", "contact.jobTitle", "contact.group", "contact.actions",
            "contact.familyName", "contact.givenName", "contact.phoneHome", "contact.phoneWork",
            "contact.birthday", "contact.province", "contact.city", "contact.district",
            "contact.address", "contact.notes", "contact.uploadAvatar", "contact.avatarHint",
            "contact.deleteConfirm", "contact.restoreConfirm", "contact.permanentDelete",
            "contact.permanentDeleteConfirm", "contact.emptyRecycle", "contact.import",
            "contact.group.new", "contact.advancedSearch", "contact.placeholder.search",
            "contact.placeholder.phone", "contact.placeholder.company", "contact.placeholder.jobTitle",
            "contact.placeholder.province", "contact.placeholder.city", "contact.placeholder.district",
            "contact.placeholder.address", "contact.placeholder.notes",
            "contact.chooseAvatar", "contact.searchByName", "contact.searchByPhone",
            "gender.male", "gender.female", "gender.unspecified",
            "group.createPrompt", "group.createFailed", "group.updateFailed", "group.deleteFailed",
            "group.deleteConfirm", "group.edit", "group.delete", "group.all", "group.rename", "group.renamePrompt",
            "btn.save", "btn.update", "btn.uploadAndSave", "btn.reset", "btn.cancel",
            "btn.confirm", "btn.confirmRestore", "btn.confirmDelete", "btn.confirmEmpty", "btn.confirmReset",
            "btn.restore", "btn.permanentDelete", "btn.back", "btn.backHome", "btn.emptyRecycle", "btn.addUser",
            "modal.addContact", "modal.editContact", "modal.uploadAvatar", "modal.confirmDelete",
            "modal.confirmRestore", "modal.permanentDelete", "modal.emptyRecycle", "modal.addUser", "modal.resetPwd",
            "recycle.title", "recycle.empty", "recycle.name", "recycle.phone", "recycle.email",
            "recycle.deletedAt", "recycle.actions",
            "admin.title", "admin.id", "admin.username", "admin.email", "admin.role", "admin.status",
            "admin.createdAt", "admin.actions", "admin.role.admin", "admin.role.user",
            "admin.status.enabled", "admin.status.disabled",
            "admin.btn.enable", "admin.btn.disable", "admin.btn.delete", "admin.btn.resetPwd",
            "admin.deleteConfirm", "admin.placeholder.newPassword", "admin.error.pwdTooShort",
            "error.title", "error.serverError", "error.resourceNotFound", "error.pageNotFound",
            "common.loading", "common.networkError", "common.success", "common.failure", "common.records",
            "alert.success", "alert.failure", "alert.saveSuccess", "alert.deleteSuccess",
            "alert.restoreSuccess", "alert.emptySuccess", "alert.exportAll", "alert.networkError",
            "alert.updateSuccess", "alert.resetPwdSuccess",
            "profile.title", "profile.avatar", "profile.changeAvatar", "profile.savePassword",
            "profile.oldPassword", "profile.placeholder.oldPassword", "profile.oldPasswordWrong", "profile.saveSuccess"
        };

        Map<String, String> messages = new TreeMap<>();
        for (String key : keys) {
            try {
                messages.put(key, messageSource.getMessage(key, null, locale));
            } catch (Exception ignored) {
            }
        }
        return ApiResult.success(messages);
    }
}
