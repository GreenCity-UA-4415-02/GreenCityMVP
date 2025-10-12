package greencity.webcontroller;

import greencity.annotations.CurrentUser;
import greencity.annotations.ValidLanguage;
import greencity.dto.habit.HabitAssignDto;
import greencity.dto.user.UserVO;
import greencity.service.HabitAssignService;
import greencity.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import springfox.documentation.annotations.ApiIgnore;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Controller
@AllArgsConstructor
@RequestMapping("/user")
@CrossOrigin
public class UserHabitsController {
    private final HabitAssignService habitAssignService;
    private final UserService userService;

    @GetMapping("/habits")
    public String getUserHabitsPage(
        @CurrentUser UserVO currentUser,
        Model model,
        @ApiIgnore @ValidLanguage Locale locale) {

        Long userId = currentUser.getId();

        List<HabitAssignDto> acquiredHabits = habitAssignService
            .getAllHabitAssignsByUserIdAndStatusAcquired(userId, locale.getLanguage());
        List<HabitAssignDto> inProgressHabits = habitAssignService
            .findInprogressHabitAssignsOnDateContent(userId, LocalDate.now(), locale.getLanguage());
        List<HabitAssignDto> cancelledHabits = habitAssignService
            .getAllHabitAssignsByUserIdAndCancelledStatus(userId, locale.getLanguage());
        List<HabitAssignDto> customHabits = habitAssignService
            .getAllCustomHabitAssignsByUserId(userId, locale.getLanguage());

        List<UserVO> topFriends = userService.getSixFriendsWithTheHighestRating(userId);
        int friendsCount = userService.getFriendsCount(userId);

        model.addAttribute("user", currentUser);
        model.addAttribute("acquiredHabits", acquiredHabits);
        model.addAttribute("inProgressHabits", inProgressHabits);
        model.addAttribute("cancelledHabits", cancelledHabits);
        model.addAttribute("customHabits", customHabits);
        model.addAttribute("topFriends", topFriends);
        model.addAttribute("friendsCount", friendsCount);

        return "user/my_habits";
    }
}