import com.cardinalblue.approutes.AppRoutes;
import com.cardinalblue.approutes.Callback;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;


public class AppRoutesTest {
    private AppRoutes mAppRoutes;

    @Before
    public void setUp() {
        System.out.println("setUp");
        mAppRoutes = new AppRoutes();
        Callback defaultCallback = new Callback() {
            @Override
            public void call(Map<String, Object> parameters) {
                System.out.println("parameters: " + parameters);
            }
        };
        mAppRoutes.addRoute("app:/collages/feeds/:feed_name", defaultCallback);
        mAppRoutes.addRoute("app:/collages/:collage_id/echoes", defaultCallback);
        mAppRoutes.addRoute("app:/collages/:collage_id", defaultCallback);
        mAppRoutes.addRoute("app:/users/:user_id/followed_users", defaultCallback);
        mAppRoutes.addRoute("app:/users/:user_id/followers", defaultCallback);
        mAppRoutes.addRoute("app:/sticker_store/:bundle_id", defaultCallback);
        mAppRoutes.addRoute("app:/sticker_store", defaultCallback);
    }

    @Test
    public void testUserRouting() {
        assertTrue(mAppRoutes.routeUrl("app:/users/123"));
    }

    @Test
    public void testCollageRouting() {
        assertTrue(mAppRoutes.routeUrl("app:/collages/123"));
    }

    @Test
    public void testFeedsRouting() {
        assertTrue(mAppRoutes.routeUrl("app:/collages/feeds/contest"));
    }
}