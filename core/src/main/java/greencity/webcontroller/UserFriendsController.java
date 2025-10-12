package greencity.webcontroller;

import greencity.annotations.CurrentUser;
import greencity.dto.user.UserVO;
import greencity.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/user/friends")
@CrossOrigin
public class UserFriendsController {
    private final UserService userService;

    /**
     * Method that returns user's friends page with tabs.
     *
     * @param currentUser {@link UserVO} of current user
     * @param model       Model that will be configured and returned to user
     * @param tab         Active tab parameter (all, find, requests)
     * @return View template path {@link String}
     */
    @GetMapping
    public String getUserFriendsPage(
        @CurrentUser UserVO currentUser,
        Model model,
        @RequestParam(value = "tab", defaultValue = "all") String tab) {
        
        Long userId = currentUser.getId();
        
        // Get friends data based on active tab
        switch (tab) {
            case "all":
                model.addAttribute("allFriends", userService.getAllUserFriends(userId));
                break;
            case "find":
                model.addAttribute("recommendedFriends", userService.getRecommendedFriends(userId));
                break;
            case "requests":
                model.addAttribute("pendingRequests", userService.getPendingFriendRequests(userId));
                break;
        }
        
        model.addAttribute("user", currentUser);
        model.addAttribute("activeTab", tab);
        model.addAttribute("friendsCount", userService.getFriendsCount(userId));
        
        return "user/my_eco_friends";
    }

    /**
     * Method that searches for users by name.
     *
     * @param currentUser {@link UserVO} of current user
     * @param name        Search term
     * @return List of matching users
     */
    @GetMapping("/search")
    @ResponseBody
    public ResponseEntity<List<UserVO>> searchUsers(
        @CurrentUser UserVO currentUser,
        @RequestParam String name) {
        
        List<UserVO> searchResults = userService.searchUsersByName(name, currentUser.getId());
        return ResponseEntity.ok(searchResults);
    }

    /**
     * Method that sends a friend request.
     *
     * @param currentUser {@link UserVO} of current user
     * @param friendId    ID of the friend to send request to
     * @return Success response
     */
    @PostMapping("/request")
    @ResponseBody
    public ResponseEntity<String> sendFriendRequest(
        @CurrentUser UserVO currentUser,
        @RequestParam Long friendId) {
        
        boolean success = userService.sendFriendRequest(currentUser.getId(), friendId);
        if (success) {
            return ResponseEntity.ok("Friend request sent successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to send friend request");
        }
    }

    /**
     * Method that accepts a friend request.
     *
     * @param currentUser {@link UserVO} of current user
     * @param friendId    ID of the friend who sent the request
     * @return Success response
     */
    @PostMapping("/accept")
    @ResponseBody
    public ResponseEntity<String> acceptFriendRequest(
        @CurrentUser UserVO currentUser,
        @RequestParam Long friendId) {
        
        boolean success = userService.acceptFriendRequest(friendId, currentUser.getId());
        if (success) {
            return ResponseEntity.ok("Friend request accepted successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to accept friend request");
        }
    }

    /**
     * Method that rejects a friend request.
     *
     * @param currentUser {@link UserVO} of current user
     * @param friendId    ID of the friend who sent the request
     * @return Success response
     */
    @PostMapping("/reject")
    @ResponseBody
    public ResponseEntity<String> rejectFriendRequest(
        @CurrentUser UserVO currentUser,
        @RequestParam Long friendId) {
        
        boolean success = userService.rejectFriendRequest(friendId, currentUser.getId());
        if (success) {
            return ResponseEntity.ok("Friend request rejected successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to reject friend request");
        }
    }

    /**
     * Method that removes a friend.
     *
     * @param currentUser {@link UserVO} of current user
     * @param friendId    ID of the friend to remove
     * @return Success response
     */
    @PostMapping("/remove")
    @ResponseBody
    public ResponseEntity<String> removeFriend(
        @CurrentUser UserVO currentUser,
        @RequestParam Long friendId) {
        
        boolean success = userService.removeFriend(currentUser.getId(), friendId);
        if (success) {
            return ResponseEntity.ok("Friend removed successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to remove friend");
        }
    }
}
